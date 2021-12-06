const userAjaxUrl = "admin/users/";

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: userAjaxUrl,
    updateTable: function () {
        $.get(userAjaxUrl, drawTable);
    }
};

// $(document).ready(function () {
$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "paging": false,
            "info": true,
            "columns": [
                {
                    "data": "name"
                },
                {
                    "data": "email"
                },
                {
                    "data": "roles"
                },
                {
                    "data": "enabled"
                },
                {
                    "data": "registered"
                },
                {
                    "defaultContent": "Edit",
                    "orderable": false
                },
                {
                    "defaultContent": "Delete",
                    "orderable": false
                }
            ],
            "order": [
                [
                    0,
                    "asc"
                ]
            ]
        })
    );
});

function enableRow(id, cb) {
    let isChecked = cb.checked;
    $.ajax({
        url: ctx.ajaxUrl + id,
        type: "POST",
        data: {enabled: isChecked}
    }).then(
        // done
        function () {
            $(cb).closest('tr').attr("data-user-enabled", isChecked);
            successNoty(isChecked ? "Enabled" : "Disabled");
        },
        // fail
        function () {
            $(cb).prop("checked", !isChecked);
        }
    );
}
