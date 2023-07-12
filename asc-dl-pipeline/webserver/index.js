import express from 'express';
import DataHelper from './src/utils/dataHelper.js'
import path from 'path';
import { fileURLToPath } from 'url';
import startListener, {registerSIGINT} from './src/utils/startListener.js';
import storePid from './src/utils/storePid.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

if (!process.env['PRIVEE_PATH']) {
  console.log("\n====================================================");
  console.log("You need to set the PRIVEE_PATH environment variable");
  console.log("====================================================\n");
  process.exit(1);
}
const rootPath = process.env['PRIVEE_PATH'] + '/listener/';
let pyPid = -1;

if (process.argv[2] && process.argv[2] === '-l') {
  console.log("Got argument -l, so Starting the listener");
  pyPid = startListener(rootPath);
}
registerSIGINT();

console.log(`PIDs are:\n \t node: ${process.pid},\n\t python: ${pyPid}`);
storePid('/tmp/privee_pid', process.pid);

const app = express()
// const port = 3001
const port = 41070

const dataHelper = new DataHelper(rootPath + '/data');


app.get('/data', (req, res) => {
  res.json(dataHelper.data)
})

app.get('/hist', (req, res) => {
  res.json(dataHelper.histCharts)
})

// serve the react app files
app.use(express.static(`${__dirname}/react-web/build`));

// serve static in path
// app.use('/_website', express.static(`${__dirname}/react-web/build`));

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`)
})
