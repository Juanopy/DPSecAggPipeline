import React from 'react';
import Plot from 'react-plotly.js';

const Graph = ({histData, currData, name, axLabels}) => {
    const axes = {
        ...(axLabels ? {
            yaxis: {
                title: {
                    // text: "Times seen"
                    text: "Häufigkeit Personenanzahl"
                },
            },
            xaxis: {
                title: {
                    text: "Personenanzahl (#Devices)"
                }
            }
        } : null),
    };

    return <Plot
        style={{
            flex: 1,
            maxHeight: '100%',
            maxWidth: '50%',
        }}
        config={{
            displayModeBar: false,
            displaylogo: false,
        }}
        data={[
            {
                type: 'bar',
                name: 'Synthetic History',
                y: Object.values(histData),
                x: Object.keys(histData),
                orientation:'v',
                hoverinfo: 'none',
            },
            {
                type: 'bar',
                name: 'Current',
                y: Object.values(currData),
                x: Object.keys(currData),
                orientation:'v',
                hoverinfo: 'none',
                error_y: {
                    type: 'constant',
                    value: 1,
                    visible: true,
                },
            },
        ]}
        layout={{
            title: name,
            barmode: 'stack',
            showlegend: true,
            ...axes,
        }}
    />
}

export default Graph