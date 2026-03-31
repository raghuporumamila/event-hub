(function () {
  function setActiveEventTab(tabName) {
    $('#definitionsId').attr('class', tabName === 'definitions' ? 'nav-link active' : 'nav-link');
    $('#eventsId').attr('class', tabName === 'events' ? 'nav-link active' : 'nav-link');
    $('#testId').attr('class', tabName === 'test' ? 'nav-link active' : 'nav-link');
  }

  if (window.__eventsBeforeRequestHandler) {
    document.body.removeEventListener('htmx:beforeRequest', window.__eventsBeforeRequestHandler);
  }

  window.__eventsBeforeRequestHandler = function (event) {
    var tab = event.target && event.target.getAttribute ? event.target.getAttribute('data-tab') : null;
    if (tab) {
      setActiveEventTab(tab);
    }
  };

  document.body.addEventListener('htmx:beforeRequest', window.__eventsBeforeRequestHandler);
  htmx.ajax('GET', '/site/v1/definitions', '#cardContent');
})();
