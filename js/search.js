let buses = [];

// Existing schedule and price details
const busInfo = {
    "Musivo Express": {
        departure: "06:30 AM",
        arrival: "01:30 PM",
        duration: "7h",
        price: 799
    },
    "GreenLine Travels": {
        departure: "08:00 AM",
        arrival: "03:30 PM",
        duration: "7h 30m",
        price: 599
    },
    "CityConnect": {
        departure: "09:45 PM",
        arrival: "05:45 AM",
        duration: "8h",
        price: 699
    },
    "Tamil Express": {
        departure: "10:30 PM",
        arrival: "06:00 AM",
        duration: "7h 30m",
        price: 649
    },
    "South Star": {
        departure: "11:00 PM",
        arrival: "06:30 AM",
        duration: "7h 30m",
        price: 549
    }
};


// Get search details
const params = new URLSearchParams(location.search);

const from =
    params.get("from") ||
    localStorage.getItem("fromCity") ||
    "Chennai";

const to =
    params.get("to") ||
    localStorage.getItem("toCity") ||
    "Coimbatore";

const date =
    localStorage.getItem("travelDate") ||
    "Today";


// Save search details
localStorage.setItem("fromCity", from);
localStorage.setItem("toCity", to);


// Display search details
document.getElementById("from").textContent = from;
document.getElementById("to").textContent = to;
document.getElementById("date").textContent = date;


// Fetch buses from Java backend
async function loadBuses() {

    try {

        const response = await fetch("http://localhost:8081/buses");

        if (!response.ok) {
            throw new Error("Backend response error");
        }

        const data = await response.json();

        // Convert backend data into frontend format
        buses = data.map(bus => {

            const info = busInfo[bus.name] || {
                departure: "Available",
                arrival: "Available",
                duration: "N/A",
                price: 0
            };

            return {
                id: bus.id,
                name: bus.name,
                busNumber: bus.busNumber,
                type: bus.type,
                source: bus.source,
                destination: bus.destination,
                contact: bus.contact,

                departure: info.departure,
                arrival: info.arrival,
                duration: info.duration,
                price: info.price
            };
        });

        render();

    } catch (error) {

        console.error(error);

        document.getElementById("busResults").innerHTML =
            "<div class='bookingCard'>" +
            "Unable to load buses. Please start Bus Backend." +
            "</div>";
    }
}


// Display buses
function render() {

    const activeFilters =
        [...document.querySelectorAll(".filter:checked")]
            .map(x => x.value);


    const list = buses.filter(bus =>
        activeFilters.every(filter =>
            bus.type.includes(filter)
        )
    );


    document.getElementById("busResults").innerHTML =

        list.map(bus => `

            <div class="busCard">

                <div>
                    <div class="busName">
                        ${bus.name}
                    </div>

                    <div class="muted">
                        ${bus.type}
                    </div>

                    <div class="muted">
                        Bus No: ${bus.busNumber}
                    </div>
                </div>


                <div>
                    <div class="time">
                        ${bus.departure}
                    </div>

                    <div class="muted">
                        ${bus.duration}
                    </div>
                </div>


                <div>
                    <div class="time">
                        ${bus.arrival}
                    </div>

                    <div class="muted">
                        Arrival
                    </div>
                </div>


                <div>
                    <div class="price">
                        ₹${bus.price}
                    </div>

                    <button
                        class="select"
                        onclick="selectBus(${bus.id})">
                        Select
                    </button>
                </div>

            </div>

        `).join("")


        || `

            <div class="bookingCard">
                No buses match your filters.
            </div>

        `;
}


// Select bus
function selectBus(id) {

    const selectedBus =
        buses.find(bus => bus.id === id);

    if (!selectedBus) {
        alert("Bus not found");
        return;
    }


    localStorage.setItem(
        "selectedBus",
        JSON.stringify(selectedBus)
    );


    location.href = "bus-details.html";
}


// Filter handling
document
    .querySelectorAll(".filter")
    .forEach(filter => {

        filter.onchange = render;

    });


// Start loading buses
loadBuses();