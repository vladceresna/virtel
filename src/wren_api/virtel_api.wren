class Result {
	construct ok(data) {
		_data = data
		_status = true
	}
	construct error(msg) {
		_msg = msg
		_status = false
	}
	status { _status }
	msg { _msg }
	data { _data }
	unwrap() {
		if (_status) {
			return _data
		} else {
			Log.error(_msg)
			Fiber.abort(_msg)
		}
	}
	unwrapOr(default) {
		if (_status) {
			return _data
		} else {
			Log.warning(_msg)
			return default
		}
	}
	unwrapOrElse(closure) {
        if (_status) {
            return _data
        } else {
            return closure.call(_msg)
        }
    }
    then(closure) {
        if (_status) {
    		return closure.call(_data)
        } else {
		    return this
        }
	}
	consume(onSuccess, onError) {
		if (_status) {
            onSuccess.call(_data)
		} else {
            onError.call(_msg)
		}
	}
}
class Log {
    foreign static info(msg)
    foreign static success(msg)
    foreign static error(msg)
    foreign static warning(msg)
}
class VirtelApp {
    foreign static start()
}
class VirtelPlugin {
    foreign static start()
}
class Center {
    foreign static startApp(id)
}
class Permissions {
    foreign static request(id)
    foreign static check(id)
}
