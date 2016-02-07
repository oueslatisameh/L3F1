var schedule = require('node-schedule');

exports.startLaunch = function(config, pg, clientRedis) {

	var ruleStat = new schedule.RecurrenceRule();
	ruleStat.dayOfWeek = config.schedule.statDay;
	ruleStat.hour = config.schedule.statHour;
	ruleStat.minute = config.schedule.statMin;
	ruleStat.second = config.schedule.statSec;

	var ruleProduct = new schedule.RecurrenceRule();
	ruleProduct.dayOfWeek = config.schedule.productDay;
	ruleProduct.hour = config.schedule.productHour;
	ruleProduct.minute = config.schedule.productMin;
	ruleProduct.second = config.schedule.productSec;

	var functionss = functions(config, pg, clientRedis);
	schedule.scheduleJob(ruleStat, functionss.funcStat);
	schedule.scheduleJob(ruleProduct, functionss.funcProduct);
	return (functionss);
};

function functions(config, pg, clientRedis) {
	var element = {
		funcStat : function() {
			var asyncEach = require('async');
			asyncEach.each(config.stats, function(statName, callback) {
				var asyncWaterfall = require('async');
				asyncWaterfall.waterfall([
					function(callback) {
						clientRedis.get("stats:" + statName, function(err, resultNumber) {
							console.log(resultNumber + " " + typeof resultNumber);
							callback(null, (resultNumber || "0"));
						});
					},
					function(resultNumber, callback) {
						clientRedis.del("stats:" + statName, function(err, result) {
							callback(null, resultNumber);
						});
					},
					function(resultNumber, callback) {
						var queryPG = 'INSERT INTO stats VALUES(\'' + statName + 
							'\', current_date, ' + resultNumber + ');';
						console.log(queryPG);
						pg(queryPG, function(err, result) {
							if (err) {
								callback(err);
							} else {
								callback(null, result);
							}
						});
					},
					function(result) {
						console.log("new entries : " + statName + " " + result);
					}
				], function(err) {
					console.log(err);
				});
			}, function(err) {
				if (!err) {
					console.log("les nouvelles valeurs sont entree");
				} else {
					console.log(err);
				}
			});		
		},
		funcProduct : function() {
			var async = require('async');
			async.waterfall([
				function(callback) {
					clientRedis.keys("product:*", function(err, result) {
						if (err)
							callback("je sors dans keys product:");
						else
							callback(null, result);
					});
				},
				function(resultKeys, callback) {
					console.log("resultKeys =" + resultKeys);
					if (resultKeys.length != 0)
						clientRedis.del(resultKeys, function(err, result) {
							if (err)
								callback("dans del product:*");
							else
								callback(null);
						});
					else
						callback(null);
				},
				function(callback) {
					clientRedis.keys("circle:product:*", function(err, result) {
						if (err) {
							callback("je sors dans circle:product");
						} else {
							callback(null, result);
						}
					});
				},
				function(resultKeys, callback) {
					console.log("resultKeys =" + resultKeys);
					if (resultKeys.length != 0) {
						clientRedis.del(resultKeys, function(err, result) {
							if (err) {
								callback("dans del circle:product:*");
							} else {
								callback(null);
							}
						});
					} else {
						callback(null);
					}
				},
				function() {
					console.log("fin de la suppression");
				}
			], function(err) {
				console.log(err);
				console.log("erreur du serveur");
			});
		}
	};
	return (element);
}