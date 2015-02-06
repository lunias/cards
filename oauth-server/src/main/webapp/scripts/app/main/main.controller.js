'use strict';

angular.module('cardsOauthApp')
    .controller('MainController', function ($scope, Principal) {
    	
        $scope.isAuthenticated = Principal.isAuthenticated;
    	$scope.isInRole = Principal.isInRole;
    	
        Principal.identity().then(function(account) {
            $scope.account = account;
        });
        
        $scope.users = [{
        	'firstName': 'Ethan',
        	'lastName': 'Anderson'
        } , {
        	'firstName': 'Mike',
        	'lastName': 'Campbell'
        }];
        
    });
