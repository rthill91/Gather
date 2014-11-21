var app = angular.module('app', []);

app.controller('mainController', function($scope) {

});

app.controller('registerController', function($scope, $log) {
	$scope.user = {};

	function validateForm() {
		if(angular.isUndefinedOrNull($scope.user.username) &&
		angular.isUndefinedOrNull($scope.user.password) &&
		$scope.user.password === $scope.confirmPassword) {
			return true;
		} else return false;
	}

	$scope.submitForm = function() {
		if(validateForm()) {
			$log.debug("Send To API");
		}
	}
});

app.controller('loginController', function($scope, $log) {
	$scope.login = {};

	$scope.submitForm = function() {
		if(validateForm()) {
			$log.debug("Send To API");
		}
	}
});
