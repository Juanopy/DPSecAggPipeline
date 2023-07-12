const mapping = {
    "tram": "Innenraum",
    "bus": 'Innenraum',
    "metro": 'Innenraum',
    "park": 'Park',
    "airport": 'Flughafen',
    "public_square": "Öffentlicher Platz",
}

const allLabels = [
    "airport",
    "bus",
    "metro",
    "metro_station",
    "park",
    "public_square",
    "shopping_mall",
    "street_pedestrian",
    "street_traffic",
    "tram"
]

/**
 * Maps a label to needed labels
 * @param {string[]} arr An event as string array
 * @returns The event with mapped label
 */
export default function mapData(arr) {
    const out = [...arr];
    const k = out[2];
    if (k in mapping) {
        out[2] = mapping[k];
    } else {
        out[2] = 'null';
    }
    return out;
}

export const filterData = (arr) => arr[2] !== 'null';
