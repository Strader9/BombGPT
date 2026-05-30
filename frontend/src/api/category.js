import axios from 'axios'

export function getCategoryList(){

    return axios.get(
        'http://localhost:8080/category/list'
    )
}
