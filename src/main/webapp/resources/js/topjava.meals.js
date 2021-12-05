const mealAjaxUrl = "user/meals/";

const ctx = {
    ajaxUrl: mealAjaxUrl,
    filterMapping: "filter",
    filterParams: {
        startDate: "",
        endDate: "",
        startTime: "",
        endTime: ""
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
    $('#dateTime').datetimepicker();
    $('#startDate').datetimepicker({timepicker: false, format: 'Y-m-d'});
    $('#endDate').datetimepicker({timepicker: false, format: 'Y-m-d'});
    $('#startTime').datetimepicker({datepicker: false, format: 'H:i'});
    $('#endTime').datetimepicker({datepicker: false, format: 'H:i'});
});

function setFilter() {
    ctx.filterParams =
        {
            startDate: $("#startDate").val(),
            endDate: $("#endDate").val(),
            startTime: $("#startTime").val(),
            endTime: $("#endTime").val()
        };
    updateTable()
}

function resetFilter() {
    $("#filter").get(0).reset();
    setFilter();
}