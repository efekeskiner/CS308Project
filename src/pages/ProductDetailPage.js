import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { addToCart } from "../services/cart";
import { getProductById } from "../services/products";
import { getApprovedComments, submitComment } from "../services/comments";
import { getProductRating, submitRating } from "../services/ratings";
import { getCurrentUser, isLoggedIn as checkIsLoggedIn, authFetch } from "../services/auth";
import { getMyOrders } from "../services/orders";
import { isInWishlist, toggleWishlist } from "../services/wishlist";
import "./ProductDetailPage.css";

const FALLBACK_IMAGE =
  "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='420' viewBox='0 0 300 420'%3E%3Crect width='300' height='420' fill='%23f3ece3'/%3E%3Crect x='45' y='55' width='210' height='310' rx='16' fill='%23ffffff' stroke='%23d1c7bc' stroke-width='3'/%3E%3Ctext x='150' y='195' text-anchor='middle' font-family='Arial' font-size='24' fill='%236b4f3b'%3ENo Image%3C/text%3E%3Ctext x='150' y='230' text-anchor='middle' font-family='Arial' font-size='16' fill='%238b7b72'%3EBook Cover%3C/text%3E%3C/svg%3E";

const WISHLIST_API = "http://localhost:8080/api/wishlist";

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
  const [wishlisted, setWishlisted] = useState(false);

  const loggedIn = checkIsLoggedIn();
  const currentUser = getCurrentUser();
  const isCustomer = !currentUser || currentUser.role === "CUSTOMER";

  const orderList = Array.isArray(orders) ? orders : orders.content || [];

  const canReview =
    loggedIn &&
    isCustomer &&
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

        if (loggedIn && isCustomer) {
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
  }, [id, loggedIn, isCustomer]);

  useEffect(() => {
    let cancelled = false;

    const loadWishlistStatus = async () => {
      if (!product || !isCustomer) {
        setWishlisted(false);
        return;
      }

      if (!loggedIn) {
        setWishlisted(isInWishlist(product.id));
        return;
      }

      try {
        const response = await authFetch(WISHLIST_API);

        if (!response.ok) {
          throw new Error("Wishlist could not be loaded");
        }

        const data = await response.json();

        const wishlistItems = Array.isArray(data)
          ? data
          : data?.content || data?.data || data?.items || [];

        const exists = wishlistItems.some((item) => {
          const wishlistProductId =
            item.product?.id ??
            item.product?.productId ??
            item.productId ??
            item.book?.id ??
            item.bookId ??
            item.id;

          return Number(wishlistProductId) === Number(product.id);
        });

        if (!cancelled) {
          setWishlisted(exists);
        }
      } catch (err) {
        console.error("Wishlist status could not be loaded:", err);
        if (!cancelled) {
          setWishlisted(false);
        }
      }
    };

    loadWishlistStatus();

    return () => {
      cancelled = true;
    };
  }, [product, loggedIn, isCustomer]);

  const handleAddToCart = () => {
    if (!product || !inStock) return;

    addToCart(product);
  };

  const handleWishlistToggle = async () => {
    if (!product || !isCustomer) return;

    if (!loggedIn) {
      const updatedWishlist = toggleWishlist(product);
      const nowWishlisted = updatedWishlist.some(
        (item) => Number(item.id) === Number(product.id)
      );
      setWishlisted(nowWishlisted);
      return;
    }

    const previousWishlisted = wishlisted;
    const nextWishlisted = !wishlisted;

    setWishlisted(nextWishlisted);

    try {
      if (nextWishlisted) {
        const response = await authFetch(WISHLIST_API, {
          method: "POST",
          body: JSON.stringify({ productId: Number(product.id) }),
        });

        if (!response.ok) {
          throw new Error("Product could not be added to wishlist");
        }
      } else {
        const response = await authFetch(`${WISHLIST_API}/${product.id}`, {
          method: "DELETE",
        });

        if (!response.ok) {
          throw new Error("Product could not be removed from wishlist");
        }
      }
    } catch (err) {
      console.error("Wishlist could not be updated:", err);
      setWishlisted(previousWishlisted);
    }
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

  const getBookImage = (product) => {
    if (product.imageUrl) {
      return product.imageUrl;
    }

    if (product.serialNumber) {
      return `https://covers.openlibrary.org/b/isbn/${product.serialNumber}-L.jpg`;
    }

    return FALLBACK_IMAGE;
  };

  const imageSrc = getBookImage(product);

  return (
    <div className="product-detail-page">
      <button className="back-button" onClick={() => navigate("/products")}>
        ← Back to Products
      </button>

      <div className="product-detail-card">
        <div className="product-image-section">
          <img
            src={imageSrc}
            alt={product.name || "Product"}
            className="product-detail-image"
            onError={(event) => {
              event.currentTarget.src = FALLBACK_IMAGE;
            }}
          />
        </div>

        <div className="product-info-section">
          <h1>{product.name || "No Name"}</h1>
          <p className="product-category">{product.categoryName || "Uncategorized"}</p>

          <p className="product-description">
            {product.description || "No description available."}
          </p>

          <div className="price-section">
            {Number(product.discountRate) > 0 && (
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

          {isCustomer && (
            <div className="product-action-buttons">
              <button
                className={inStock ? "add-to-cart-button" : "add-to-cart-button disabled"}
                disabled={!inStock}
                onClick={handleAddToCart}
              >
                {inStock ? "Add to Cart" : "Out of Stock"}
              </button>

              <button
                className={wishlisted ? "wishlist-detail-button active" : "wishlist-detail-button"}
                onClick={handleWishlistToggle}
              >
                {wishlisted ? "♥ Remove from Wishlist" : "♡ Add to Wishlist"}
              </button>
            </div>
          )}
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
              {comment.score != null && (
                <div className="comment-rating">
                  {"★".repeat(Math.round(comment.score / 2))}{"☆".repeat(5 - Math.round(comment.score / 2))}
                  <span className="comment-rating-number">{comment.score}/10</span>
                </div>
              )}
              <p>{comment.content}</p>
            </div>
          ))
        )}
      </section>

      <section className="review-section">
        <h2>Rate & Comment</h2>

        {!loggedIn && (
          <p className="muted-text">
            You must log in to rate or comment on this product.
          </p>
        )}

        {loggedIn && !isCustomer && (
          <p className="muted-text">
            Manager accounts cannot rate, comment, or purchase products.
          </p>
        )}

        {loggedIn && isCustomer && !canReview && (
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