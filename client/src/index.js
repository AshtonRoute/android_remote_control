import 'source-map-support/register';

import Bluebird from 'bluebird';
import { exec } from 'child_process';
import { promisify } from 'util';
import chokidar from 'chokidar';

import adbClient from './config/adb';
import log from './config/logger';
import ENV from './config/environment';

const appName = 'lan.home.remote_control.Main';

const execAsync = promisify(exec);

function pushServer() {
  return new Promise(async (res, rej) => {
    try {
      const transfer = await adbClient.push(ENV.DEVICE_SERIAL, ENV.SERVER_PATH, ENV.DEVICE_SERVER_PATH);

      transfer.on('error', err => rej(err));
      transfer.on('end', () => res());
    } catch (err) {
      rej(err);
    }
  });
}

async function getAppPIDs() {
  const { stderr, stdout: output } = await execAsync([
    'adb',
    '-s',
    ENV.DEVICE_SERIAL,
    'shell',
    'ps',
    '-A',
    '-o',
    'PID',
    '-o',
    'ARGS',
  ].join(' '));

  if (stderr) {
    throw new Error(stderr);
  }

  const apps = output.split('\n')
                     .slice(1)
                     .map(v => {
                        const [pid, ...args] = v.trim().split(' ');

                        return {
                          pid: Number.parseInt(pid.trim(), 10),
                          args: args.map(v => v.trim()).join(' '),
                        };
                      })
                      .filter(v => !Number.isNaN(v.pid) && v.args && v.args.includes(appName));

  return apps;
}

async function killRunningApps(pids) {
  const { stderr } = await execAsync([
    'adb',
    '-s',
    ENV.DEVICE_SERIAL,
    'shell',
    'kill',
    ...pids,
  ].join(' '));

  if (stderr) {
    throw new Error(stderr);
  }
}

async function main(delay) {
  await adbClient.connect(ENV.DEVICE_SERIAL);

  if (delay) {
    await Bluebird.delay(delay);
  }

  const runningApps = await getAppPIDs();

  if (runningApps.length) {
    log.info(`Found ${runningApps.length} running remote control apps. Killing before starting a new one...`, runningApps);
    await killRunningApps(runningApps.map(v => v.pid));
    log.info(`${runningApps.length} remote control apps have been terminated`);
  }

  await pushServer();

  const outputStream = await adbClient.shell(ENV.DEVICE_SERIAL, [
    `CLASSPATH=${ENV.DEVICE_SERVER_PATH}`,
    'app_process',
    '/',
    appName,
    ENV.DEVICE_SERVER_PORT,
    ENV.DEVICE_SERVER_CERT_PATH,
  ].join(' '));

  outputStream.on('error', (e) => {
    log.error(e);
  });

  outputStream.pipe(process.stdout);

  let watcher = null;

  if (ENV.WATCH_SERVER_PATH) {
    watcher = chokidar.watch(ENV.SERVER_PATH).on('change', () => {
      watcher.close();
      watcher = null;

      outputStream.destroy();

      main().catch(log.error);
    });
  }

  outputStream.on('end', () => {
    log.info('Application finished');

    if (watcher) {
      log.info('Waiting for changes...');
    }
  });
}

main(1000).catch(log.error);
