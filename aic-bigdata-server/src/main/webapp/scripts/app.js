var app = angular.module("bigdataApp", [ "ngResource", "angular-loading-bar" ]);

app.controller("ServiceCtrl", function($scope, $http) {
	$scope.startService = function() {
		$http.get("api/service?command=start").success(function(data) {
			$scope.result = data;
		});
	};
	$scope.stopService = function() {
		$http.get("api/service?command=stop").success(function(data) {
			$scope.result = data;
		});
	};
	$scope.getStatus = function() {
		$http.get("api/service/status", {}).success(function(data) {
			$scope.result = data;
		});
	};
	$scope.startAnalyse = function() {
		$http.get("api/service?command=analyse", {}).success(function(data) {
			$scope.result = data;
		});
	};
});

app.factory("ConnectionService", function($http) {
	var srv = {};

	
	srv.getAllTopics = function() {
		return $http.get("api/connections/topics",{}).then(function(resp){
			console.log(resp.data);
			return resp.data;
		});
	};

	srv.findUsersByTopic = function(topic) {
		return $http.get("api/connections/topics/" + topic + "/users", {})
				.success(function(data) {
					console.log(data);
					return data;
				});
	};

	return srv;
});

app.factory("UserService", function($http) {
	var srv = {};

	srv.getConnections = function(userId) {
		return $http.get("api/users/" + userId + "/connections", {}).then(
				function(resp) {
					return resp.data;
				});
	};

	return srv;
});

app.controller("ConnectionCtrl", function($scope, ConnectionService) {
	$scope.topics = [];
	$scope.selTopic;
	
	ConnectionService.getAllTopics().then(function(data){
		console.log(data);
		$scope.topics = data;		
	});
});

app.controller("UserCtrl", function($scope, $http, UserService) {
	$scope.pageSize = 100;
	$scope.pageNumber = 0;

	function _init() {
		getUsers();
	}

	function getUsers() {
		var url = "api/users?size=" + $scope.pageSize + "&page="
				+ $scope.pageNumber;
		$http.get(url).success(function(data) {
			$scope.users = data;
		});
	}

	$scope.selectUser = function(user) {
		console.log(user);
		UserService.getConnections(user.id).then(function(data) {
			console.log(data);
		});
	}

	$scope.updateUsers = function() {
		getUsers();
	}

	_init();
});