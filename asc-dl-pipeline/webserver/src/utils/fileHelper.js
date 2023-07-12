const { exec } = require("child_process");

const execPromise = (...args) => {
    return new Promise((res, _) => {
        exec(...args, (err, stdout, stderr) => res([err, stdout, stderr]))
    })
}

const asPromise = (f, ...args) => {
    return new Promise((res, rej) => {
        f(...args, (err, out) => {
            if (err) rej(err);
            res(out);
        })
    })
}

export const lastLines = async (path, n) => {
    const res = await execPromise(`tail -n ${n} ${path}`)
    if (res[0] != null) {
        return res[1].split("\n");
    }
    return null;
}