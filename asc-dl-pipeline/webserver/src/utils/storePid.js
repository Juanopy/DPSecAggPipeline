import fs from 'fs';

export default function storePid(path, pid) {
    fs.writeFileSync(path, `${pid}`, {
        encoding: 'utf8',
        flag: 'w'
      });
}