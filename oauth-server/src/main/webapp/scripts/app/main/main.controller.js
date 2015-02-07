'use strict';

angular.module('cardsOauthApp').controller('MainController',
		function($scope, $templateCache, Principal, User) {

			$scope.isAuthenticated = Principal.isAuthenticated;
			$scope.isInRole = Principal.isInRole;

			Principal.identity().then(function(account) {
				$scope.account = account;
			});

			$templateCache.put('user-grid-row',
					"<div ng-click=\"$parent.getExternalScopes().rowClick(row.entity)\" ng-repeat=\"(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name\" class=\"ui-grid-cell hover-grid-cell\" ng-class=\"{ 'ui-grid-row-header-cell': col.isRowHeader }\" ui-grid-cell></div>"
			);
			
			$templateCache.put('user-grid-footer',
					"<div class=\"ui-grid-bottom-panel\" style=\"text-align: right\; padding: 52px 5px 0px 0px;\">Total Users: {{ getExternalScopes().totalElements }}</div>");
			
			$scope.userGridOptions = {
				externalScope: $scope.gridScope,
				infiniteScrollPercentage : 15,
				showFooter : true,
				enableFiltering : true,
				useExternalSorting: true,
				enableRowSelection: true,
				columnDefs : [ {
					field : 'login',
					allowCellFocus: false
				}, {
					field : 'firstName',
					allowCellFocus: false					
				}, {
					field : 'lastName',
					allowCellFocus: false					
				}, {
					field : 'email',
					allowCellFocus: false					
				}, {
					field : 'langKey',
					allowCellFocus: false					
				} ],
				rowHeight: 50,
				rowTemplate: 'user-grid-row',
				footerTemplate: 'user-grid-footer',
				enableHorizontalScrollbar: false,
				onRegisterApi : function(gridApi) {					
					
					gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
						
						if ($scope.currentPage < $scope.totalPages - 1) {
							++$scope.currentPage;
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
						$scope.currentPage = 0;
						$scope.sortField = sortColumns.length ? sortColumns[0].field : '';
						$scope.sortDir = sortColumns.length ? sortColumns[0].sort.direction : '';													
						
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
			};			
			
			function updateUserGrid(replaceData, success, err) {
				
				var replaceData = replaceData || false;
				var success = success || angular.noop;
				var error = err || angular.noop;
				
				var params = {
						page: $scope.currentPage
				};
				
				if ($scope.sortField) params.sort = $scope.sortField + ',' + $scope.sortDir;
				
				User.findAll(params, function(usersResponse) {
				
					$scope.currentPage = usersResponse.page.number;
					$scope.totalPages = usersResponse.page.totalPages;
					$scope.gridScope.totalElements = usersResponse.page.totalElements;
					
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
			
			$scope.currentPage = 0;
			$scope.totalPages = 0;
			$scope.sortField = '';
			$scope.sortDir = '';			
			updateUserGrid();
			
			$scope.gridScope = {
					rowClick: function (user) {
						console.log(user.login);
					}	
			};
		});
