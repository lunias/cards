'use strict';

angular.module('cardsOauthApp')
    .factory('AuthServerProvider', function loginService($http, $state, localStorageService, Base64) {
        return {
            login: function(credentials) {
                var data = "username=" + credentials.username + "&password="
                    + credentials.password + "&grant_type=password&scope=read%20write%20delete&" +
                    "client_id=oauthadmin";
                return $http.post('oauth/token', data, {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "Accept": "application/json",
                        "Authorization": "Basic " + Base64.encode("oauthadmin" + ':' + "AdminsAreTheBest")
                    }
                }).success(function (response) {
                    var expiredAt = new Date();
                    expiredAt.setSeconds(expiredAt.getSeconds() + response.expires_in);
                    response.expires_at = expiredAt.getTime();
                    localStorageService.set('token', response);
                    return response;
                });
            },
            logout: function() {
                // logout from the server
                $http.post('api/logout').then(function() {
                    localStorageService.clearAll();
                    $state.go('login');
                });
            },
            getToken: function () {
                return localStorageService.get('token');
            },
            hasValidToken: function () {
                var token = this.getToken();
                return token && token.expires_at && token.expires_at > new Date().getTime();
            }
        };
    });
