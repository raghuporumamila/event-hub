(function () {
  if (!window.__dashboardAfterSwapHandler) {
    window.__dashboardAfterSwapHandler = function (event) {
      if (event.detail.target && event.detail.target.id === 'contentArea') {
        $('.modal').modal('hide');
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
      }
    };
    document.body.addEventListener('htmx:afterSwap', window.__dashboardAfterSwapHandler);
  }

  window.initializeDashboardContent = function (workspaceName) {
    if (!workspaceName) {
      htmx.ajax('GET', '/site/v1/manageWorkspaces', '#contentArea');
      return;
    }
    htmx.ajax('GET', '/site/v1/events', '#contentArea');
  };
})();
