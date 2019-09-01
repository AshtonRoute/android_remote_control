import Joi from '@hapi/joi';

const schema = Joi.object({
  SERVER_PATH: Joi.string().default('/server/app.apk'),
  SOCKET_NAME: Joi.string().default('remote_control'),

  WATCH_SERVER_PATH: Joi.boolean().default(false),

  DEVICE_SERIAL: Joi.string().required(),
  DEVICE_SERVER_PATH: Joi.string().default('/data/local/tmp/remote_control_server.jar'),
  DEVICE_SERVER_PORT: Joi.number().integer().min(0).default(32436),
  DEVICE_SERVER_CERT_PATH: Joi.string(),
});

const { value, error } = schema.validate(process.env, {
  abortEarly: false,
  allowUnknown: true,
  stripUnknown: {
    arrays: false,
    objects: true,
  },
});

if (error) {
  throw error;
}

export default value;
