const mealAjaxUrl = "user/meals/";

const ctx = {
    ajaxUrl: mealAjaxUrl,
    updateTable: function () {
        $.get(mealAjaxUrl + "filter",
            {
                startDate: $("#startDate").val(),
                endDate: $("#endDate").val(),
                startTime: $("#startTime").val(),
                endTime: $("#endTime").val()
            },
            drawTable);
    }
};

$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "paging": false,
            "info": true,
            "columns": [
                {
                    "data": "dateTime"
                },
                {
                    "data": "description"
                },
                {
                    "data": "calories"
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
                    "desc"
                ]
            ]
        })
    );
});

function resetFilter() {
    $("#filter").get(0).reset();
    ctx.updateTable();
}

function convertDateFormat() {
    let date = new Date($("#excludedDateTime").val());
    $("#dateTime").val(date.getFullYear() + "-" + ("0" + (date.getMonth() + 1)).slice(-2) + "-" + ("0" + date.getDate()).slice(-2)
        + " " + ("0" + date.getHours()).slice(-2) + ":" + ("0" + date.getMinutes()).slice(-2));
}