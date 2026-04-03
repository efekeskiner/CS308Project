import mockProducts from "../data/mockProducts";

function getProducts() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockProducts);
    }, 500);
  });
}

export default getProducts;