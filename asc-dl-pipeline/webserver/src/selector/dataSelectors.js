import moment from 'moment';

moment.locale('de');

export const addLog = (o, e) => {
    const [tmestmp, devID, label, prob, devNr] = e;
    if (!Object.hasOwn(o, devID)) {
        o[devID] = [];
    }
    o[devID].push(`${moment(tmestmp).format('LTS')}: (${label}, ${devNr})`);
    return o;
}

export const selectLogs = (data) => {
    return data.reduce(addLog, {})
}

export const incNestedObject = (obj, path) => {
    let o = obj;
    for (let i = 0; i < path.length; i++) {
        const k = path[i];
        if (!Object.hasOwn(o, k)) {
            o[k] = i === path.length-1 ? 1 : {};
        } else if (i == path.length-1) {
            o[k] += 1;
        }
        o = o[k];
    }
    return obj;
}

export const selectChartData = (data) => {
    return data.reduce((o, e) => {
        const [tmestmp, devID, label, prob, devNr] = e;
        incNestedObject(o, [label, devNr]);
        return o;
    }, {});
};

// export const selectChartData = createSelector(
//     [selectData],
//     (data) => {
//     return data.reduce((o, e) => {
//         const [tmestmp, devID, label, prob, devNr] = e;
//         incNestedObject(o, [label, devNr]);
//         return o;
//     }, {});
// });
