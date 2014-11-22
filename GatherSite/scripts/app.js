var app = angular.module('app', []);

app.controller('mainController', function($scope) {

});

app.controller('registerController', function($scope, $http, $log) {
	$scope.user = {};

	function validateForm() {
		if(($scope.user.username) &&
		($scope.user.password) &&
		($scope.user.password === $scope.confirmPassword)) {
			
			return true;
		} else return false;
	}

	$scope.submitForm = function() {
		if(validateForm()) {
		$http.get("http://rthill91.synology.me:8081")
		.success(function(data, status, headers, config) {
			$log.log(data);
		})
		.error(function(data, status, headers, config) {
			$log.error(data);
		});
		} else {
			$log.debug("Invalid Credentials");
		}
	}
});

app.controller('loginController', function($scope, $log) {
	$scope.login = {};

	function validateForm() {
		if(($scope.login.username) &&
		($scope.login.password)) {
			return true;
		} else return false;
	}
	
	$scope.submitForm = function() {
		if(validateForm()) {
			$log.debug("Send Login To API");
		} else {
			$log.debug("Invalid Credentials");
		}
	}
});