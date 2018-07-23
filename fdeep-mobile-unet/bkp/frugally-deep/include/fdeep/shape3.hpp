// Copyright 2016, Tobias Hermann.
// https://github.com/Dobiasd/frugally-deep
// Distributed under the MIT License.
// (See accompanying LICENSE file or at
//  https://opensource.org/licenses/MIT)

#pragma once

#include "fdeep/common.hpp"

#include "fdeep/shape2.hpp"
#include "fdeep/shape3_variable.hpp"

#include <algorithm>
#include <cstddef>
#include <cstdlib>
#include <string>
#include <vector>

namespace fdeep { namespace internal
{

class shape3
{
public:
    explicit shape3(
        std::size_t depth,
        std::size_t height,
        std::size_t width) :
            depth_(depth),
            height_(height),
            width_(width)
    {
    }
    std::size_t volume() const
    {
        return depth_ * height_ * width_;
    }

    shape2 without_depth() const
    {
        return shape2(height_, width_);
    }

    std::size_t depth_;
    std::size_t height_;
    std::size_t width_;
};

inline shape3 make_shape3_with(
    const shape3& default_shape,
    const shape3_variable shape)
{
    return shape3(
        fplus::just_with_default(default_shape.depth_, shape.depth_),
        fplus::just_with_default(default_shape.height_, shape.height_),
        fplus::just_with_default(default_shape.width_, shape.width_));
}

inline bool operator == (const shape3& lhs, const shape3_variable& rhs)
{
    return
        (rhs.depth_.is_nothing() || lhs.depth_ == rhs.depth_.unsafe_get_just()) &&
        (rhs.height_.is_nothing() || lhs.height_ == rhs.height_.unsafe_get_just()) &&
        (rhs.width_.is_nothing() || lhs.width_ == rhs.width_.unsafe_get_just());
}

inline bool operator == (const std::vector<shape3>& lhss,
    const std::vector<shape3_variable>& rhss)
{
    return fplus::all(fplus::zip_with(
        [](const shape3& lhs, const shape3_variable& rhs) -> bool
        {
            return lhs == rhs;
        },
        lhss, rhss));
}

inline bool operator == (const shape3& lhs, const shape3& rhs)
{
    return
        lhs.depth_ == rhs.depth_ &&
        lhs.height_ == rhs.height_ &&
        lhs.width_ == rhs.width_;
}

inline bool operator != (const shape3& lhs, const shape3& rhs)
{
    return !(lhs == rhs);
}

inline shape3 dilate_shape3(
    const shape2& dilation_rate, const shape3& s)
{
    assertion(dilation_rate.height_ >= 1, "invalid dilation rate");
    assertion(dilation_rate.width_ >= 1, "invalid dilation rate");

    const std::size_t height = s.height_ +
        (s.height_ - 1) * (dilation_rate.height_ - 1);
    const std::size_t width = s.width_ +
        (s.width_ - 1) * (dilation_rate.width_ - 1);
    return shape3(s.depth_, height, width);
}

} // namespace internal

using shape3 = internal::shape3;

inline std::string show_shape3(const shape3& s)
{
    const std::vector<std::size_t> dimensions =
        {s.depth_, s.height_, s.width_};
    return fplus::show_cont_with_frame(", ", "(", ")", dimensions);
}

inline std::string show_shape3s(
    const std::vector<shape3>& shapes)
{
    return fplus::show_cont(fplus::transform(show_shape3, shapes));
}

} // namespace fdeep
