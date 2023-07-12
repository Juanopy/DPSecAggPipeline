import Path from 'path';
import fs from 'fs';
import {Tail} from 'tail';
import { addLog, incNestedObject, selectChartData, selectLogs } from '../selector/dataSelectors.js';
import { sortObject } from './helper.js';
import mapData, { filterData } from './dataMapping.js';
import chokidar from 'chokidar';

export default class DataHelper {
    /**
     * {string} The path to the data storage
     */
    path;

    /**
     * {string[][]} The array with raw events
     */
    dataRaw = [];

    /**
     * The object that stores the data easy accessible
     */
    data = {
        logs: [],
        charts: {},
    };

    /**
     * The object that stores the charts for history
     */
    histCharts = {}

    /**
     * The list that contain all tail references
     */
    tails = [];

    watcher = null;

    constructor(path) {
        this.path = Path.normalize(path);
        this.watcher = chokidar.watch(this.path)
        .on('ready', () => this.watcher
            .on('add', this._addFile)
            .on('unlink', this._initData)
        )
        this._initData(this.path);
    }

    _cleanTails = () => {
        this.tails.forEach(t => t.unwatch());
        this.tails = [];
    }

    _initData = () => {
        this.dataRaw = [];
        this._cleanTails();
        for (const f of this._getAllFiles()) {
            this._addFile(f);
        }
        const histFile = Path.resolve(this.path, 'hist.dat')
        if (fs.existsSync(histFile)) {
            this.histCharts = selectChartData(this._fileToRawData(histFile))
        }
    }

    _addFile = (f) => {
        this.dataRaw = this.dataRaw.concat(this._fileToRawData(f));
        this._rawDataToData();
        const t = new Tail(f);
        t.on("line", (data) => {
            this.updateData(this._stringToRaw(data));
        });
        this.tails.push(t);
        this.tails.length
    }

    updateData = (data) => {
        if (filterData(data)) {
            const [tmestmp, devID, label, prob, devNr] = data;
            this.dataRaw.push(data);
            incNestedObject(this.data.charts, [label, devNr]);
            addLog(this.data.logs, data)
            this.data.charts = sortObject(this.data.charts)
        }
    }

    /**
     * Gives all file names within this.path
     * @returns {string[]} The file names
     */
    _getAllFiles = () => {
        const p = this.path;
        const files = fs.readdirSync(p);
        const out = files
            .filter(f => !fs.lstatSync(Path.resolve(p, f)).isDirectory())
            .filter(f => f !== 'hist.dat')
            .map(f => Path.join(p, f));
        return out;
    }

    /**
     * Gives how many devices are registered
     */
    getNumOfDevice = () => {
        this.getAllDataFiles().length;
    }

    /**
     * Gives the raw data events for each file
     * @param {string} f The filename
     * @returns {[]} all events as raw data
     */
    _fileToRawData = (f) => {
        const data = fs.readFileSync(f, 'utf8');
        return data
            .trim()
            .split("\n")
            .map(this._stringToRaw)
            .filter(filterData)
    }

    /**
     * 
     * @param {string} str The raw string of an event
     * @returns {string[]} An event that cnsist out of multiple strings
     */
    _stringToRaw = (str) => {
        return mapData(str.split(","))
    }

    /**
     * Extracts the log and chart data out of the raw events
     */
    _rawDataToData = () => {
        this.data.logs = selectLogs(this.dataRaw);
        this.data.charts = sortObject(selectChartData(this.dataRaw));
    }
}