
const chunkArray = (arr, size) => {
    const chunkedArray = [];
    for (let i = 0; i < arr.length; i++) {
        const last = chunkedArray[chunkedArray.length - 1];
        if (!last || last.length === size) {
            chunkedArray.push([arr[i]]);
        } else {
            last.push(arr[i]);
        }
    };
    return chunkedArray;
};

export default chunkArray;