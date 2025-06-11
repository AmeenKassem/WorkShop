const axios = require("axios");
const qs = require("qs");
const api = "http://10.0.0.6:8080";
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

async function addStore(token) {
  const tokenAdder = token;
  const req = qs.stringify({
    token: tokenAdder,
    storeName: "masho masho",
    category: "Electronics",
  });
  const responseReg = await axios.post(api + "/api/store/addStore", req, {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
  });
  return responseReg.data.data;
}

// loginUsers();
// const tokenUser =tokenMap['user1'];
// console.log(tokenUser)
// const storeId = addStore(tokenUser);

const products = [
  // Electronics
  [
    "iPhone 14 Pro Max",
    "Electronics",
    "Apple flagship smartphone with A16 chip",
    "phone",
  ]
  ,
  [
    "Google Pixel 8",
    "Electronics",
    "Google's latest smartphone with AI camera features",
    "phone",
  ],
  [
    "OnePlus 11",
    "Electronics",
    "Fast and smooth Android smartphone",
    ["phone"],
  ],
  [
    "Xiaomi 13 Pro",
    "Electronics",
    "High-performance phone with Leica camera",
    "phone",
  ],
  [
    "Galaxy A71 FE",
    "Electronics",
    "Affordable Samsung phone with premium features",
    "phone",
  ],
  ["Galaxy S23 Ultra", "Electronics", "High-end Samsung smartphone", "phone"],
  [
    "Sony WH-1000XM5",
    "Electronics",
    "Noise-cancelling headphones",
    "headphones",
  ],
  [
    "iPad Pro 12.9",
    "Electronics",
    "Large screen tablet for professionals",
    "tablet",
  ],

  // HOME
  [
    "Cozy Bean Bag",
    "HOME",
    "Comfortable seating for your living room",
    "furniture",
  ],
  ["LED Floor Lamp", "HOME", "Bright floor lamp with 3 color modes", "lamp"],
  ["Wall-mounted Bookshelf", "HOME", "Space-saving modern bookshelf", "shelf"],

  // FASHION
  ["Slim Fit Blazer", "FASHION", "Stylish blazer for formal events", "blazer"],
  ["Nike Air Max", "FASHION", "Popular running shoes", "shoes"],
  ["Leather Handbag", "FASHION", "Elegant handbag for daily use", "handbag"],

  // CAMERA
  ["Canon EOS R10", "CAMERA", "Mirrorless camera with 24MP sensor", "camera"],
  ["GoPro HERO11", "CAMERA", "Action camera for adventure lovers", "gopro"],
  ["Sony Alpha ZV-E10", "CAMERA", "Vlogging camera with flip screen", "vlog"],

  // HOME_APPLIANCE
  [
    "Dyson V11 Vacuum",
    "HOME_APPLIANCE",
    "Cordless powerful vacuum cleaner",
    "vacuum",
  ],
  [
    "Samsung Air Purifier",
    "HOME_APPLIANCE",
    "Removes dust and allergens",
    "air",
  ],
  [
    "Bosch Dishwasher",
    "HOME_APPLIANCE",
    "Energy-efficient dishwasher",
    "dishwasher",
  ],

  // KITCHEN
  ["NutriBullet Blender", "KITCHEN", "Blender for smoothies", "blender"],
  ["Instant Pot Duo", "KITCHEN", "Electric pressure cooker", "pressure"],
  ["Chef’s Knife Set", "KITCHEN", "Premium stainless steel knives", "knife"],

  // GARDEN
  ["Electric Lawn Mower", "GARDEN", "Quiet and eco-friendly", "lawn"],
  ["Hose Reel Cart", "GARDEN", "Organize your garden hose", "hose"],
  ["LED Solar Garden Lights", "GARDEN", "Eco lighting for outdoors", "lights"],

  // OFFICE
  [
    "Ergonomic Office Chair",
    "OFFICE",
    "Comfortable chair with lumbar support",
    "chair",
  ],
  ["Standing Desk", "OFFICE", "Adjustable height desk", "desk"],
  ["Logitech MX Master 3", "OFFICE", "Precision wireless mouse", "mouse"],

  // AUTOMOTIVE
  [
    "Car Jump Starter",
    "AUTOMOTIVE",
    "Portable jump starter for emergencies",
    "starter",
  ],
  ["Magnetic Phone Mount", "AUTOMOTIVE", "Mount for dashboard", "mount"],
  ["Tire Inflator", "AUTOMOTIVE", "Electric pump for car tires", "inflator"],

  // TOYS
  ["LEGO Star Wars", "TOYS", "Buildable Star Wars ship", "lego"],
  ["Barbie Dreamhouse", "TOYS", "3-story dollhouse", "barbie"],
  ["RC Monster Truck", "TOYS", "Remote control stunt truck", "rc"],
];

// loginUsers();
// addStore('eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwidXNlcjFcIixcImlkXCI6Mn0iLCJpYXQiOjE3NDc4OTY0NDgsImV4cCI6MTc0NzkwMDA0OH0.oD0Sz5lENWmcl_g2IlItzYsh0F6wpxaA-JD9voH6tWk')
// addProductAndItem(
//   tokenUser,
//   2,
//   "galaxy a71 fe",
//   "Electronics",
//   "aasdasd",
//   "phones",
//   5,
//   10
// );
// loginUsers();
const tokenUSer =
  "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwidXNlcjFcIixcImlkXCI6NH0iLCJpYXQiOjE3NDk2NTY4MzgsImV4cCI6MTc0OTY2MDQzOH0.CMMdwjiZ1JXa2n9LrrqbzJVKqRK7Z7CXgRySeS_qyFU";
// addStore(tokenUSer)
products.forEach(([name, category, description, keyword]) => {
  addProductAndItem(tokenUSer, 1, name, category, description, keyword, 5, 10);
});
