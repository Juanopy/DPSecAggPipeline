
export const incNestedObject = (obj, path) => {
    let o = obj;
    for (let i = 0; i < path.length; i++) {
        const k = path[i];
        if (!Object.hasOwn(o, k)) {
            o[k] = i === path.length - 1 ? 1 : {};
        } else if (i == path.length - 1) {
            o[k] += 1;
        }
        o = o[k];
    }
    return obj;
}

export const sortObject = (obj, compareFn) => {
    return Object.keys(obj)
        .sort(compareFn)
        .reduce((res, key) => (res[key] = obj[key], res), {})
};