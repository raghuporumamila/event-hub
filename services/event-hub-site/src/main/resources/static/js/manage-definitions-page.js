(function () {
  function setDeleteDefinitionId(id) {
    $('#deleteDefinitionId').val(id);
  }

  function prepareDeleteDefinition() {
    if (!$('#deleteDefinitionId').val()) {
      $('#deleteDefinitionId').val($('table tbody input[type="checkbox"]:checked').first().val());
    }
    return !!$('#deleteDefinitionId').val();
  }

  $(document)
    .off('click.manageDefinitionsDelete')
    .on('click.manageDefinitionsDelete', '.definition-delete-trigger', function () {
      setDeleteDefinitionId(this.getAttribute('data-definition-id'));
    });

  $('#deleteDefinitionForm')
    .off('submit.manageDefinitionsDeleteSubmit')
    .on('submit.manageDefinitionsDeleteSubmit', function () {
      return prepareDeleteDefinition();
    });

  if (!window.__manageDefinitionsAfterSwapHandler) {
    window.__manageDefinitionsAfterSwapHandler = function (event) {
      if (event.detail.target && event.detail.target.id === 'editModal') {
        var schema = document.getElementById('schema');
        if (!schema) {
          return;
        }
        try {
          schema.value = JSON.stringify(JSON.parse(schema.value), undefined, 4);
        } catch (e) {
          // Keep existing value if schema isn't valid JSON.
        }
      }
    };
    document.body.addEventListener('htmx:afterSwap', window.__manageDefinitionsAfterSwapHandler);
  }
})();
