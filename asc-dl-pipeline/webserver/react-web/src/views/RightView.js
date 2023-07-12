import React from 'react';
import StyleSheet from 'reactjs-stylesheet';
import { useImmer } from 'use-immer';
import Graph from '../components/Graph';
import useData from '../hooks/useData';
import chunkArray from '../utils/chunkArray';
import { sortFn } from '../utils/dataMapping';

const chartsInRow = 2;
const rowsPerView = 2;
const axLabelOnlyAt1st = false;

const RightView = () => {
    const [showHist, setShowHist] = useImmer(true);
    const toggleShowHist = () => setShowHist(!showHist);
    const {charts, hist} = useData();

    return <div
        style={{
            borderRadius: 25,
            flex: 3,
            flexDirection: 'column',
            display: 'flex',
            height: '80vh',
            width: '100%',
            backgroundColor: 'white',
        }}
        >
            <div style={{
                display: 'flex',
                justifyContent: 'space-around',
                flexDirection: 'row',
                paddingTop: '2vh',
            }}>
                <div style={{color: 'black', textAlign: 'center'}}>Verteilung über die Häufigkeit der Personenanzahl pro Umgebung</div>
                <button style={{
                    backgroundColor: '#004b5a',
                    color: 'white',
                    borderRadius: 10,
                    paddingLeft: '1vw',
                    paddingRight: '1vw',
                    cursor: 'pointer',
                    fontSize: '50%',
                    opacity: showHist ? 1 : 0.5,
                }} onClick={toggleShowHist}>History</button>
            </div>
        <div
        style={{
            flex: 1,
            flexDirection: 'column',
            display: 'flex',
            height: '100%',
            width: '100%',
            overflowY: 'scroll',
        }}
        >
            {
                chunkArray(Object.keys(charts).sort(sortFn), chartsInRow).map((keys, i) => (
                    <div key={String(keys)} style={styles.graphWrapper}>
                        {keys.map((k, j) => (
                            <Graph
                                axLabels={!axLabelOnlyAt1st || j === 0}
                                key={k}
                                currData={charts[k]}
                                histData={showHist ? (hist[k] || {}): {}}
                                name={k}
                            />
                        ))}
                    </div>
                ))
            }
        </div>
    </div>
}

const styles = StyleSheet.create({
    graphWrapper: {
        flex: 1,
        display: 'flex',
        minHeight: `${100/rowsPerView}%`,
        flexDirection: 'row',
    }
});

export default RightView