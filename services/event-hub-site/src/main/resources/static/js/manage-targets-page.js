(function () {
  function showEditTargetDialog(element) {
    $('#editTargetId').val(element.getAttribute('data-target-id'));
    $('#editTargetName').val(element.getAttribute('data-target-name'));
    $('#editTargetType').val(element.getAttribute('data-target-type'));
    $('#editTargetCredentials').val(element.getAttribute('data-target-credentials') || '');
  }

  $(document)
    .off('click.manageTargetsEdit')
    .on('click.manageTargetsEdit', '.target-edit-trigger', function () {
      showEditTargetDialog(this);
    });
})();
