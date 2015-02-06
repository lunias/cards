'use strict';

angular.module('cardsOauthApp').factory('User', function($resource) {
	
    return $resource('api/users/:login', {}, {
    	
        'findAll': { method: 'GET', isArray: false,
        	interceptor: {
                response: function(response) {
                    return {                    	
                    	body: response.data._embedded.userResourceList,
                    	links: response.data._links,
                    	page: response.data.page
                    };
                }        		
        	}
        },
        
        'update': { method: 'PUT' }
    });	
});