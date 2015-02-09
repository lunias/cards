'use strict';

angular.module('cardsOauthApp').controller('EditUserModalController', function($scope, $modalInstance, user, field) {		
	
	$scope.user = {};
	angular.extend($scope.user, user);
	
	$scope.field = field;
	
	$scope.save = function () {
		
		// close modal and pass back modified user
		$modalInstance.close($scope.user);
	};

	$scope.delete = function() {
		
		// TODO confirm delete
		console.log('confirm delete');
		// close modal and pass back user okay'ed for deletion
		$modalInstance.close(
				{
					user: $scope.user, 
					remove: true
				}
		);		
	};
	
	$scope.cancel = function () {
		
		$modalInstance.dismiss('cancel');
	};	
	
});