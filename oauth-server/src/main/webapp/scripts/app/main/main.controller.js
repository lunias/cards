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
			var sortField = '';
			var sortDir = '';
			
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
						
						updateUserGrid(false, function(data) {
							gridApi.infiniteScroll.dataLoaded();
							
						}, function(err) {
							gridApi.infiniteScroll.dataLoaded();
						});
					});
					
					gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
						
						// TODO support multi column sort
						sortField = sortColumns.length ? sortColumns[0].field : '';
						sortDir = sortColumns.length ? sortColumns[0].sort.direction : '';
						currentPage = 0;
							
						// scroll to top
						gridApi.cellNav
						.scrollTo(
								grid,
								$scope,
								$scope.userGridOptions.data[0],
								$scope.userGridOptions.columnDefs[0]);						
						
						updateUserGrid(true);												
						
						grid.options.loadTimout = false;
					});
				} 
			}
			
			function updateUserGrid(replaceData, success, err) {
				
				var replaceData = replaceData || false;
				var success = success || angular.noop;
				var error = err || angular.noop;
				
				var params = {
						page: currentPage
				};
				
				if (sortField) params.sort = sortField + ',' + sortDir;
				
				User.findAll(params, function(usersResponse) {
				
					currentPage = usersResponse.page.number;
					totalPages = usersResponse.page.totalPages;					
					
					if (replaceData) {
						$scope.userGridOptions.data = usersResponse.body;						
					} else {						
						$scope.userGridOptions.data = usersResponse.body.reduce(function(coll, item) {
							coll.push(item);
							return coll;
						}, $scope.userGridOptions.data);
					}
					
					success(usersResponse);
					
				}, function(err) {					
					error(err);					
				});								
			}
			
			updateUserGrid();

		});
