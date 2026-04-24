import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { addToCart } from "../services/cart";
import { getProductById } from "../services/products";
import { getApprovedComments, submitComment } from "../services/comments";
import { getProductRating, submitRating } from "../services/ratings";
import { getMyOrders } from "../services/orders";
import "./ProductDetailPage.css";

function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [comments, setComments] = useState([]);
  const [ratingInfo, setRatingInfo] = useState(null);
  const [orders, setOrders] = useState([]);

  const [selectedRating, setSelectedRating] = useState(0);
  const [commentText, setCommentText] = useState("");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reviewMessage, setReviewMessage] = useState("");

  const token = localStorage.getItem("accessToken");
  const isLoggedIn = !!token;

  const orderList = Array.isArray(orders) ? orders : orders.content || [];

  const canReview =
    isLoggedIn &&
    orderList.some((order) => {
      const status = String(order.status || "").toUpperCase();
      const items = order.items || [];

      return (
        status === "DELIVERED" &&
        items.some((item) => Number(item.productId) === Number(id))
      );
    });

  useEffect(() => {
    async function loadProductDetail() {
      try {
        setLoading(true);
        setError("");

        const productData = await getProductById(id);
        setProduct(productData);

        try {
          const commentsData = await getApprovedComments(id);
          setComments(Array.isArray(commentsData) ? commentsData : commentsData.content || []);
        } catch (commentError) {
          console.error(commentError);
          setComments([]);
        }

        try {
          const ratingData = await getProductRating(id);
          setRatingInfo(ratingData);
        } catch (ratingError) {
          console.error(ratingError);
          setRatingInfo(null);
        }

        if (isLoggedIn) {
          try {
            const orderData = await getMyOrders();
            setOrders(orderData);
          } catch (orderError) {
            console.error(orderError);
            setOrders([]);
          }
        }
      } catch (err) {
        console.error(err);
        setError("Product details could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadProductDetail();
  }, [id, isLoggedIn]);

  const handleAddToCart = () => {
    if (!product || !product.inStock) return;

    addToCart(product);
    alert("Product added to cart.");
  };

  const handleSubmitReview = async (event) => {
    event.preventDefault();

    if (!selectedRating) {
      setReviewMessage("Please select a rating.");
      return;
    }

    try {
      await submitRating(id, selectedRating);

      if (commentText.trim()) {
        await submitComment(id, commentText.trim());
      }

      setReviewMessage(
        "Your review was submitted. Your comment will be visible after product manager approval."
      );
      setSelectedRating(0);
      setCommentText("");

      try {
        const updatedRating = await getProductRating(id);
        setRatingInfo(updatedRating);
      } catch (ratingError) {
        console.error(ratingError);
      }
    } catch (err) {
      console.error(err);
      setReviewMessage("Review could not be submitted.");
    }
  };

  if (loading) {
    return <div className="product-detail-page">Loading product...</div>;
  }

  if (error) {
    return <div className="product-detail-page">{error}</div>;
  }

  if (!product) {
    return <div className="product-detail-page">Product not found.</div>;
  }

  const inStock = product.inStock ?? product.quantityInStock > 0;
  const averageRating =
    ratingInfo?.averageRating ?? product.averageRating ?? "No rating yet";
  const ratingCount = ratingInfo?.ratingCount ?? product.ratingCount;

  return (
    <div className="product-detail-page">
      <button className="back-button" onClick={() => navigate("/products")}>
        ← Back to Products
      </button>

      <div className="product-detail-card">
        <div className="product-image-section">
          <img
            src={product.imageUrl || "https://via.placeholder.com/300?text=No+Image"}
            alt={product.name || "Product"}
            className="product-detail-image"
          />
        </div>

        <div className="product-info-section">
          <h1>{product.name || "No Name"}</h1>
          <p className="product-category">{product.categoryName || "Uncategorized"}</p>

          <p className="product-description">
            {product.description || "No description available."}
          </p>

          <div className="price-section">
            {product.discountRate > 0 && (
              <span className="original-price">
                {product.originalPrice} TL
              </span>
            )}

            <span className="discounted-price">
              {product.price} TL
            </span>
          </div>

          <div className="info-grid">
            <p><strong>Model:</strong> {product.model || "-"}</p>
            <p><strong>ISBN:</strong> {product.serialNumber || "-"}</p>
            <p><strong>Distributor:</strong> {product.distributorInfo || "-"}</p>
            <p><strong>Warranty:</strong> {product.warrantyStatus || "-"}</p>
            <p><strong>Stock:</strong> {inStock ? product.quantityInStock : "Out of stock"}</p>
            <p>
              <strong>Average Rating:</strong> {averageRating}
              {ratingCount !== undefined && ratingCount !== null ? ` (${ratingCount})` : ""}
            </p>
          </div>

          <button
            className={inStock ? "add-to-cart-button" : "add-to-cart-button disabled"}
            disabled={!inStock}
            onClick={handleAddToCart}
          >
            {inStock ? "Add to Cart" : "Out of Stock"}
          </button>
        </div>
      </div>

      <section className="comments-section">
        <h2>Approved Comments</h2>

        {comments.length === 0 ? (
          <p className="muted-text">No approved comments yet.</p>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="comment-card">
              <div className="comment-header">
                <strong>{comment.userName || "Customer"}</strong>
                {comment.createdAt && <span>{new Date(comment.createdAt).toLocaleDateString()}</span>}
              </div>
              <p>{comment.content}</p>
            </div>
          ))
        )}
      </section>

      <section className="review-section">
        <h2>Rate & Comment</h2>

        {!isLoggedIn && (
          <p className="muted-text">
            You must log in to rate or comment on this product.
          </p>
        )}

        {isLoggedIn && !canReview && (
          <p className="muted-text">
            You can review this product only after purchasing it and receiving delivery.
          </p>
        )}

        {canReview && (
          <form onSubmit={handleSubmitReview} className="review-form">
            <label>Rating</label>

            <div className="rating-widget">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((score) => (
                <button
                    key={score}
                    type="button"
                    className={selectedRating === score ? "score-button selected" : "score-button"}
                    onClick={() => setSelectedRating(score)}
                >
                    {score}
                </button>
             ))}
            </div>

            {selectedRating > 0 && (
                <p className="selected-rating-text">
                    Selected rating: {selectedRating}/10
                </p>
            )}

            <label>Comment</label>
            <textarea
              placeholder="Write your comment..."
              value={commentText}
              onChange={(event) => setCommentText(event.target.value)}
            />

            <button type="submit" className="submit-review-button">
              Submit Review
            </button>
          </form>
        )}

        {reviewMessage && <p className="review-message">{reviewMessage}</p>}
      </section>
    </div>
  );
}

export default ProductDetailPage;