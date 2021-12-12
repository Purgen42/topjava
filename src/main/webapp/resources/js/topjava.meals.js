const mealAjaxUrl = "profile/meals/";

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: mealAjaxUrl,
    updateTable: function () {
        $.ajax({
            type: "GET",
            url: mealAjaxUrl + "filter",
            data: $("#filter").serialize()
        }).done(updateTableByData);
    }
}

function clearFilter() {
    $("#filter")[0].reset();
    $.get(mealAjaxUrl, updateTableByData);
}

function convertAndSave() {
    $('#hiddenDateTime').val($('#dateTime').val().replace(" ", "T"));
    save();
}

$.ajaxSetup({
    converters: {
        "text json": function (str) {
            var json = JSON.parse(str, function(k, v) {
                if (k === "dateTime") {
                    return v.replace("T", " ");
                }
                return v;
            });
            return json;
        }
    }
});

$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "ajax": {
                "url": mealAjaxUrl,
                "dataSrc": ""
            },
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
                    "orderable": false,
                    "defaultContent": "",
                    "render": renderEditBtn
                },
                {
                    "orderable": false,
                    "defaultContent": "",
                    "render": renderDeleteBtn
                }
            ],
            "order": [
                [
                    0,
                    "desc"
                ]
            ],
            "createdRow": function (row, data, dataIndex) {
                $(row).attr("data-meal-excess", data.excess);
            }
        })
    );
});

$('#dateTime').datetimepicker({
        format: 'Y-m-d H:i',
    });
$('#startDate').datetimepicker({timepicker: false, format: 'Y-m-d'});
$('#endDate').datetimepicker({timepicker: false, format: 'Y-m-d'});
$('#startTime').datetimepicker({datepicker: false, format: 'H:i'});
$('#endTime').datetimepicker({datepicker: false, format: 'H:i'});
