
const labelOrder = [
    "Innenraum",
    "Park",
    "Flughafen",
    "Öffentlicher Platz",
]

export const sortFn = (k1, k2) => labelOrder.indexOf(k1) - labelOrder.indexOf(k2)

export const minObject = labelOrder.reduce((o, l) => ({...o, [l]: {}}), {})