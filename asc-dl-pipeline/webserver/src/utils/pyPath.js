import {execSync} from 'child_process';

export default function getPyPath() {
  return execSync('which python', {encoding: 'utf8'}).split("\n")[0];
};
