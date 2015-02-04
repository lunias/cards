'use strict';

angular.module('cardsOauthApp')
    .controller('MainController', function ($scope, Principal) {
    	
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        
    });
