(function () {
  function setDeleteSourceId(id) {
    $('#deleteSourceId').val(id);
  }

  function showEditSourceDialog(element) {
    $('#editSourceId').val(element.getAttribute('data-source-id'));
    $('#editSourceName').val(element.getAttribute('data-source-name'));
    $('#editSourceType').val(element.getAttribute('data-source-type'));
  }

  function prepareDeleteSource() {
    if (!$('#deleteSourceId').val()) {
      $('#deleteSourceId').val($('table tbody input[type="checkbox"]:checked').first().val());
    }
    return !!$('#deleteSourceId').val();
  }

  $(document)
    .off('click.manageSourcesEdit')
    .on('click.manageSourcesEdit', '.source-edit-trigger', function () {
      showEditSourceDialog(this);
    });

  $(document)
    .off('click.manageSourcesDelete')
    .on('click.manageSourcesDelete', '.source-delete-trigger', function () {
      setDeleteSourceId(this.getAttribute('data-source-id'));
    });

  $('#deleteSourceForm')
    .off('submit.manageSourcesDeleteSubmit')
    .on('submit.manageSourcesDeleteSubmit', function () {
      return prepareDeleteSource();
    });
})();
