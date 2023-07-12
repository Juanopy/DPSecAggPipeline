import { spawn } from 'child_process'
import getPyPath from './pyPath.js'
import sleep from './sleep.js';

let pyListener = null;
export default function startListener(path) {
    console.log("Starting Python listener, close all with CTRL+C\n\n");
    pyListener = spawn(getPyPath(), [`${path}/main.py`, ]);
    pyListener.stdout.on('data', (data) => {
        console.log(`Python: ${data}`);
      });

    pyListener.stderr.on('data', (data) => {
        console.error(`Python: ${data}`);
    });

    return pyListener.pid;
}

export function registerSIGINT() {
    process.on('SIGINT', _stop);
}

async function _stopListener() {
    if (pyListener) {
        pyListener.kill('SIGINT');
        await sleep(500);
    }
}

async function _stop() {
    console.log('Received SIGINT. Closing python listener and self.');
    await _stopListener();
    process.exit(0);
}
