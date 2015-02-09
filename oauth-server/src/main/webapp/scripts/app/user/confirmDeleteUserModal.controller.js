'use strict';

angular.module('cardsOauthApp').controller('ConfirmDeleteUserModalController', function($scope, $modalInstance, user) {

	$scope.user = user;
	
	$scope.confirmDelete = function () {
		
		$modalInstance.close();
	};
	
	$scope.cancelDelete = function() {
		
		$modalInstance.dismiss('cancel');
	};	
	
});