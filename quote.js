
function getQuote() {

    fetch('http://localhost:8080/quote/random')
    .then((res) => res.json())
    .then((data) => {
        const item = data.find(obj => obj.id === 1);

        console.log(item.quote)
    } )
    .catch((error) => {
        console.log("Error",error)

    })

}


getQuote();
