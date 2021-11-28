    curl -v http://localhost:8080/topjava/rest/meals

    curl -v http://localhost:8080/topjava/rest/meals/100002

    curl -v "http://localhost:8080/topjava/rest/meals/filter?startdate=2020-01-30&starttime=10:00&enddate=2020-01-30&endtime=20:00"

    curl -v "http://localhost:8080/topjava/rest/meals/filter?startdate=&starttime=&enddate=&endtime="

    curl -v -d "{\"id\":100002,\"dateTime\":\"2020-01-30T10:02:00\",\"description\":\"Updated breakfast\",\"calories\":200}" -H "Content-Type: application/json; charset=windows-1251" -X PUT http://localhost:8080/topjava/rest/meals/100002

    curl -v -d "{\"dateTime\":\"2020-02-01T18:00:00\",\"description\":\"Созданный ужин\",\"calories\":300}" -H "Content-Type: application/json; charset=windows-1251" -X POST http://localhost:8080/topjava/rest/meals

    curl -v -X DELETE http://localhost:8080/topjava/rest/meals/100002