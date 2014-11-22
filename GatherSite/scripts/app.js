var app = angular.module('app', []);

app.controller('mainController', function($scope) {

});

app.controller('registerController', function($scope, $log) {
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
			$log.debug("Send Register To API");
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