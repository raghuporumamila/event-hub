function initManageCrudTable() {
  if (!window.jQuery) {
    return;
  }

  // Activate tooltips where present
  window.jQuery('[data-toggle="tooltip"]').tooltip();

  var checkbox = window.jQuery('table tbody input[type="checkbox"]');
  window.jQuery('#selectAll').off('click').on('click', function () {
    checkbox.each(function () {
      this.checked = !!window.jQuery('#selectAll').prop('checked');
    });
  });

  checkbox.off('click').on('click', function () {
    if (!this.checked) {
      window.jQuery('#selectAll').prop('checked', false);
    }
  });
}

if (window.jQuery) {
  window.jQuery(function () {
    initManageCrudTable();
  });
}
