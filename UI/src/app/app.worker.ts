/// <reference lib="webworker" />

import { environment } from '../environments/environment';

addEventListener('message', ({ data }) => {
  let response = `worker response to ${data} and ${environment.baseUrl}`;

  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function () {
    if (this.readyState == 4 && this.status == 200) {
      response = JSON.parse(xhttp.responseText);
      if (response['success'] && response['data']) {
        let gaArray = response['data'].map((res) => {
          return {
            'name': res.projectName,
            'id': res.id,
          }
        });
        postMessage(gaArray);
      }
    }
  };
  xhttp.open('GET', `${environment.baseUrl}/api/basicconfigs/all`, true);
  xhttp.setRequestHeader('Authorization', data);
  xhttp.send();
});
