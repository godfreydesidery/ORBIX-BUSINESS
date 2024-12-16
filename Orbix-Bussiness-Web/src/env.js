
var apiUrl = (function () {
  return {
    getApiUrl: function () {
      return "http://51.20.119.45:8080/orbix-business-api"
    },
    getDevApiUrl: function () {
      return "http://127.0.0.1:8081/orbix-business-api"
    }
  }
})(apiUrl || {})

var apiUrlDev = (function () {
  return {
    getApiUrl: function () {
      return "http://127.0.0.1:8081/orbix-business-api"
    }
  }
})(apiUrlDev || {})

