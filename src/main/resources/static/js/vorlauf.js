function updateTable(game, number) {
    const row1 = document.getElementById(`p${number}-r1`)
    const row2 = document.getElementById("p" + number + "-r2")
    const row3 = document.getElementById("p" + number + "-r3")
    const row4 = document.getElementById("p" + number + "-r4")
    const summary = document.getElementById(`p${number}-summary`)

    const sets = game.sets
    const sumData = {}
    sumData.anzahlGespielteWuerfe = game.numberOfWurf
    sumData.volleScore = game.totalVolle
    sumData.abraeumenScore = game.totalAbraeumen
    sumData.score = game.totalScore
    sumData.anzahlFehler = game.totalFehlwurf
    updateRow(row1, sets[0])
    updateRow(row2, sets[1])
    updateRow(row3, sets[2])
    updateRow(row4, sets[3])
    updateRow(summary, sumData)
}

function updateRow(row, data) {
    const cells = row.children
    cells[0].innerHTML = data.anzahlGespielteWuerfe
    cells[1].innerHTML = data.volleScore
    cells[2].innerHTML = data.abraeumenScore
    cells[3].innerHTML = data.score
    cells[4].innerHTML = data.anzahlFehler
}

function updatePicture(picture, number) {
    const svg = document.getElementById(`picture-${number}-svg`)
    const circles = svg.querySelectorAll("circle")
    for (let i = 0; i < circles.length; i++) {
        circles[i].classList.remove("pin-hit", "pin-stand")
    }
    const code = picture.bildEncoded
    const one = (code & 1) != 0;
    const two = ((code >> 1) & 1) != 0;
    const three = ((code >> 2) & 1) != 0;
    const four = ((code >> 3) & 1) != 0;
    const five = ((code >> 4) & 1) != 0;
    const six = ((code >> 5) & 1) != 0;
    const seven = ((code >> 6) & 1) != 0;
    const eight = ((code >> 7) & 1) != 0;
    const nine = ((code >> 8) & 1) != 0;

    circles[0].classList.add(one ? "pin-hit" : "pin-stand")
    circles[1].classList.add(two ? "pin-hit" : "pin-stand")
    circles[2].classList.add(three ? "pin-hit" : "pin-stand")
    circles[3].classList.add(four ? "pin-hit" : "pin-stand")
    circles[4].classList.add(five ? "pin-hit" : "pin-stand")
    circles[5].classList.add(six ? "pin-hit" : "pin-stand")
    circles[6].classList.add(seven ? "pin-hit" : "pin-stand")
    circles[7].classList.add(eight ? "pin-hit" : "pin-stand")
    circles[8].classList.add(nine ? "pin-hit" : "pin-stand")
}

function updateTeamRanking(teamRankingData, teamsData) {
    const teamRanking = document.getElementById('team-ranking');
    for (let i = 0; i < Object.keys(teamRankingData).length; i++) { // teams
        const teamName = Object.keys(teamRankingData)[i];
        const players = teamRankingData[teamName];
        const teamData = teamsData.find(team => team.name === teamName);
        const teamCol = teamRanking.querySelectorAll(".col")[i];
        for (let j = 0; j < players.length; j++) { // players
            const player = players[j];
            const playerData = teamData.players.find(p => p.completeName === player);
            const card = teamCol.children[j + 1];
            const cardHeader = card.querySelector('.card-header');
            cardHeader.children[0].textContent = `${j + 1}. ${playerData.completeName}`;
            const cardBody = card.querySelector('.card-body');
            cardBody.children[0].textContent = playerData.game.totalScore;
        }
    }
}