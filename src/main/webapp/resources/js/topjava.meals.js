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

$.ajaxSetup({
    converters: {
        "text json": function (str) {
            var json = JSON.parse(str, function(k, v) {
                if (k === "dateTime") {
                    return v.substr(0, 16).replace('T', ' ');
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
$('#startDate').datetimepicker({
    timepicker: false,
    format: 'Y-m-d',
    onSelectDate: function(){
        $('#endDate').datetimepicker('setOptions',{
            minDate: $('#startDate').val() ? $('#startDate').val() : false
        })
    }
});
$('#endDate').datetimepicker({
    timepicker: false,
    format: 'Y-m-d',
    onSelectDate: function(){
        $('#startDate').datetimepicker('setOptions',{
            maxDate: $('#endDate').val() ? $('#endDate').val() : false
        })
    }
});
$('#startTime').datetimepicker({
    datepicker: false,
    format: 'H:i',
    onSelectTime: function(){
        $('#endTime').datetimepicker('setOptions',{
            minTime: $('#startTime').val() ? $('#startTime').val() : false
        })
    }
});
$('#endTime').datetimepicker({
    datepicker: false,
    format: 'H:i',
    onSelectTime: function(){
        $('#startTime').datetimepicker('setOptions',{
            maxTime: $('#endTime').val() ? $('#endTime').val() : false
        })
    }
});
