export default function sleep(ms) {
    return new Promise((res, rej) => {
        setTimeout(res, ms);
    })
}