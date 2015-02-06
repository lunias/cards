'use strict';

angular.module('cardsOauthApp').controller('MainController',
		function($scope, Principal, User) {

			$scope.isAuthenticated = Principal.isAuthenticated;
			$scope.isInRole = Principal.isInRole;

			Principal.identity().then(function(account) {
				$scope.account = account;
			});

			var totalPages = 0;
			var currentPage = 0;
			
			$scope.userGridOptions = {
				infiniteScrollPercentage : 15,
				showGridFooter : true,
				enableFiltering : true,
				columnDefs : [ {
					field : 'login'
				}, {
					field : 'firstName'
				}, {
					field : 'lastName'
				}, {
					field : 'email'
				}, {
					field : 'langKey'
				} ],
				onRegisterApi : function(gridApi) {
					gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
						
						if (currentPage < totalPages - 1) {
							++currentPage;
						} else {
							return;
						}
						
						updateUserGrid(function(data) {
							console.log(data);
							gridApi.infiniteScroll.dataLoaded();
							
						}, function(err) {
							console.log(err);
							gridApi.infiniteScroll.dataLoaded();
						});
					});
				} 
			}
			
			function updateUserGrid(success, err) {
				
				var success = success || angular.noop;
				var error = err || angular.noop;
				
				User.findAll({page: currentPage}, function(usersResponse) {
				
					currentPage = usersResponse.page.number;
					totalPages = usersResponse.page.totalPages;					
					
					$scope.userGridOptions.data = usersResponse.body.reduce(function(coll, item) {
						coll.push(item);
						return coll;
					}, $scope.userGridOptions.data);					
					
					success(usersResponse);
					
				}, function(err) {
					
					error(err);
					
				});								
			}
			
			updateUserGrid();

		});
