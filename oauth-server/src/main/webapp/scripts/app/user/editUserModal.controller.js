'use strict';

angular.module('cardsOauthApp').controller('EditUserModalController', function($scope, $modal, $modalInstance, user, field, availableRoles) {		
	
	$scope.user = {};
	angular.extend($scope.user, user);
	
	$scope.field = field;
	
	$scope.availableRoles = availableRoles;	
	
	$scope.save = function () {
		
		$modalInstance.close($scope.user);
	};

	$scope.delete = function () {		
		
		var confirmModalInstance = $modal.open({			
			templateUrl: 'scripts/app/user/deleteUserConfirm.html',
			controller: 'ConfirmDeleteUserModalController',
			size: 'sm',
			resolve: {
				user: function() {
					return $scope.user;
				}
			}
		});
		
		confirmModalInstance.result.then(function() {
			
			// close modal and pass back user okay'ed for deletion
			$modalInstance.close(
					{
						user: $scope.user, 
						remove: true
					}
			);
		});
				
	};	
	
	$scope.cancel = function () {
		
		$modalInstance.dismiss('cancel');
	};		
	
});