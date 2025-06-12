const axios = require("axios");
const qs = require("qs");
const api = "http://132.73.233.229:8080";
const users = [
  { username: "user1", password: "pass1" },
  { username: "user2", password: "pass2" },
  { username: "user3", password: "pass3" },
  { username: "user4", password: "pass4" },
  { username: "user5", password: "pass5" },
];

const loginEndpoint = api + "/api/users/login";
const tokenMap = {}; // Map of username -> token

async function generateGuestToken() {
  const response = await axios.get(api + "/api/users/generateGuest");
  return response.data.data;
}

async function loginUsers() {
  for (const user of users) {
    try {
      const guestToken = await generateGuestToken();
      console.log(guestToken);
      const formDataReg = qs.stringify({
        token: guestToken,
        username: user.username,
        password: user.password,
        age: 30,
      });

      const responseReg = await axios.post(
        api + "/api/users/register",
        formDataReg,
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      console.log(responseReg.data.data);

      const formData = qs.stringify({
        token: guestToken,
        username: user.username,
        password: user.password,
      });

      const response = await axios.post(loginEndpoint, formData, {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      });

      const token = response.data.data;
      tokenMap[user.username] = token;
      console.log(`✅ ${user.username} logged in with token: ${token}`);
      return true;
    } catch (err) {
      console.error(
        `❌ Failed login for ${user.username}:`,
        err.response?.data || err.message
      );
      return false;
    }
  }

  console.log("\n=== Token Map ===");
  console.log(tokenMap);
}

// loginUsers();

const BASE_URL_STORE = api + "/stock";

async function addProductAndItem(
  token1,
  storeId1,
  name1,
  category1,
  description1,
  keywords1,
  quantity1,
  price1
) {
  try {
    // Step 1: Add the product
    const productRes = await axios.post(
      `${BASE_URL_STORE}/addProduct`,
      qs.stringify({
        token: token1,
        name: name1,
        category: category1,
        description: description1,
        keywords: keywords1,
      }),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );

    const productId1 = productRes.data.data;
    console.log(`✅ Product added successfully. ID: ${productId1}`);

    // Step 2: Add item to store
    const itemRes = await axios.post(
      `${BASE_URL_STORE}/addItem`,
      qs.stringify({
        storeId: storeId1,
        token: token1,
        productId: productId1,
        quantity: quantity1,
        price: price1,
        category: category1,
      }),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );

    console.log("✅ Item added successfully:", itemRes.data);
  } catch (error) {
    if (error.response) {
      console.error(
        `❌ Error (${error.response.status}):`,
        error.response.data
      );
    } else {
      console.error("❌ Error:", error.message);
    }
  }
}

async function addStore(token, name) {
  const tokenAdder = token;
  const req = qs.stringify({
    token: tokenAdder,
    storeName: name,
    category: "Electronics",
  });
  const responseReg = await axios.post(api + "/api/store/addStore", req, {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  });
  return await responseReg.data.data;
}

// loginUsers();
// const tokenUser =tokenMap['user1'];
// console.log(tokenUser)
// const storeId = addStore(tokenUser);
const products = [
  // Electronics
  [
    "iPhone 14 Pro Max",
    "Electronics", // Matches enum
    "Apple flagship smartphone with A16 chip",
    "phone", // Changed to single string
  ],
  [
    "Google Pixel 8",
    "Electronics", // Matches enum
    "Google's latest smartphone with AI camera features",
    "phone", // Changed to single string
  ],
  [
    "OnePlus 11",
    "Electronics", // Matches enum
    "Fast and smooth Android smartphone",
    "phone", // Changed to single string
  ],
  [
    "Xiaomi 13 Pro",
    "Electronics", // Matches enum
    "High-performance phone with Leica camera",
    "phone", // Changed to single string
  ],
  [
    "Galaxy A71 FE",
    "Electronics", // Matches enum
    "Affordable Samsung phone with premium features",
    "phone", // Changed to single string
  ],
  [
    "Galaxy S23 Ultra",
    "Electronics", // Matches enum
    "High-end Samsung smartphone",
    "phone", // Changed to single string
  ],
  [
    "Sony WH-1000XM5",
    "Electronics", // Matches enum
    "Noise-cancelling headphones",
    "headphones", // Changed to single string
  ],
  [
    "iPad Pro 12.9",
    "Electronics", // Matches enum
    "Large screen tablet for professionals",
    "tablet", // Changed to single string
  ],

  // CAMERA -> Mapped to Electronics (or add 'Camera' to your enum)
  [
    "Canon EOS R10",
    "Electronics", // Changed from CAMERA to Electronics
    "Mirrorless camera with 24MP sensor",
    "camera", // Changed to single string
  ],
  [
    "GoPro HERO11",
    "Electronics", // Changed from CAMERA to Electronics
    "Action camera for adventure lovers",
    "gopro,action_camera", // Changed to single string, comma-separated
  ],
  [
    "Sony Alpha ZV-E10",
    "Electronics", // Changed from CAMERA to Electronics
    "Vlogging camera with flip screen",
    "vlog,camera", // Changed to single string
  ],

  // HOME
  [
    "Cozy Bean Bag",
    "Home", // Matches enum (assuming case-insensitivity on backend or map 'HOME' to 'Home')
    "Comfortable seating for your living room",
    "furniture,bean_bag", // Changed to single string
  ],
  [
    "LED Floor Lamp",
    "Home", // Matches enum
    "Bright floor lamp with 3 color modes",
    "lamp,lighting", // Changed to single string
  ],
  [
    "Wall-mounted Bookshelf",
    "Home", // Matches enum
    "Space-saving modern bookshelf",
    "shelf,bookshelf,storage", // Changed to single string
  ],

  // FASHION -> Mapped to Clothing
  [
    "Slim Fit Blazer",
    "Clothing",
    "Stylish blazer for formal events",
    "blazer,clothing",
  ], // Changed category and keywords
  ["Nike Air Max", "Clothing", "Popular running shoes", "shoes,sneakers,nike"], // Changed category and keywords
  [
    "Leather Handbag",
    "Clothing",
    "Elegant handbag for daily use",
    "handbag,accessories",
  ], // Changed category and keywords

  // HOME_APPLIANCE -> Mapped to Home (or add 'HomeAppliance' to your enum)
  [
    "Dyson V11 Vacuum",
    "Home", // Changed from HOME_APPLIANCE to Home
    "Cordless powerful vacuum cleaner",
    "vacuum,cleaning", // Changed to single string
  ],
  [
    "Samsung Air Purifier",
    "Home", // Changed from HOME_APPLIANCE to Home
    "Removes dust and allergens",
    "air_purifier,home_comfort", // Changed to single string
  ],
  [
    "Bosch Dishwasher",
    "Home", // Changed from HOME_APPLIANCE to Home
    "Energy-efficient dishwasher",
    "dishwasher,kitchen_appliance", // Changed to single string
  ],

  // KITCHEN -> Mapped to Home (or add 'Kitchen' to your enum)
  [
    "NutriBullet Blender",
    "Home",
    "Blender for smoothies",
    "blender,kitchen_appliance",
  ], // Changed category and keywords
  [
    "Instant Pot Duo",
    "Home",
    "Electric pressure cooker",
    "pressure_cooker,instant_pot",
  ], // Changed category and keywords
  [
    "Chef’s Knife Set",
    "Home",
    "Premium stainless steel knives",
    "knife_set,cookware",
  ], // Changed category and keywords

  // GARDEN -> Mapped to Home (or add 'Garden' to your enum)
  [
    "Electric Lawn Mower",
    "Home",
    "Quiet and eco-friendly",
    "lawn_mower,gardening",
  ], // Changed category and keywords
  ["Hose Reel Cart", "Home", "Organize your garden hose", "hose,garden_tool"], // Changed category and keywords
  [
    "LED Solar Garden Lights",
    "Home",
    "Eco lighting for outdoors",
    "garden_lights,solar_lights", // Changed to single string
  ],

  // OFFICE -> Mapped to Home (or add 'Office' to your enum)
  [
    "Ergonomic Office Chair",
    "Home",
    "Comfortable chair with lumbar support",
    "office_chair,furniture", // Changed to single string
  ],
  [
    "Standing Desk",
    "Home",
    "Adjustable height desk",
    "standing_desk,office_furniture",
  ], // Changed category and keywords
  [
    "Logitech MX Master 3",
    "Home",
    "Precision wireless mouse",
    "mouse,computer_accessories", // Changed to single string
  ],

  // AUTOMOTIVE -> Mapped to Electronics (or add 'Automotive' to your enum)
  [
    "Car Jump Starter",
    "Electronics",
    "Portable jump starter for emergencies",
    "jump_starter,car_accessories", // Changed to single string
  ],
  [
    "Magnetic Phone Mount",
    "Electronics",
    "Mount for dashboard",
    "phone_mount,car_accessories", // Changed to single string
  ],
  [
    "Tire Inflator",
    "Electronics",
    "Electric pump for car tires",
    "tire_inflator,car_tool", // Changed to single string
  ],

  // TOYS
  ["LEGO Star Wars", "Toys", "Buildable Star Wars ship", "lego,star_wars,toy"], // Matches enum, keywords to single string
  ["Barbie Dreamhouse", "Toys", "3-story dollhouse", "barbie,dollhouse,toy"], // Matches enum, keywords to single string
  [
    "RC Monster Truck",
    "Toys",
    "Remote control stunt truck",
    "rc_car,monster_truck,toy", // Matches enum, keywords to single string
  ],
];

//run this for adding users :

async function runScript() {
  try {
    
    // loginUsers();
    const tokenUSer =
      "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwidXNlcjFcIixcImlkXCI6NX0iLCJpYXQiOjE3NDk3MTc2ODEsImV4cCI6MTc0OTcyMTI4MX0.nucufLfOKNLCxG3p-O3VHj-kXx5TYlMfzuUZsWQEtdQ";
    const storeData = await addStore(tokenUSer, "masho masho");
    // console.log(storeData);
    products.forEach(([name, category, description, keyword]) => {
      addProductAndItem(tokenUSer, storeData, name, category, description, keyword, 5, 10);
    });
    
  } catch (error) {
    console.error("Failed to add store:", error.message);
    if (error.response) {
      console.error("Server response data:", error.response.data);
      console.error("Server status:", error.response.status);
    }
  }
}
runScript();
